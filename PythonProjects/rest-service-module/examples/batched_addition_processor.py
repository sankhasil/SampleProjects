"""
Part of the example showing the basic us of aiserve.

This custom process uses a resource folder path.
"""

import pathlib
from typing import Type, List, Tuple

import flask_restful.reqparse

import aiserve.process


class AddMultiplyProcess(aiserve.process.BaseProcessor):
    """
    Subclassing the BaseProcesser.

    Used to implement a processor showing the contents of a file.
    """

    def __init__(
        self,
        preserver_class: Type[aiserve.preserver.BasePreserver],
        batch_size=1,
        multiplier=1,
    ):
        self.multiplier = multiplier
        super().__init__(preserver_class=preserver_class, batch_size=batch_size)

    @staticmethod
    def _kwargs_from_folder(folder_path: pathlib.Path) -> dict:
        """
        Extract the keyword arguments for the constructor from a folder path.

        Parameters
        ----------
        folder_path
            Path to the folder used for initialization.

        Returns
        -------
        dict
            Keyword arguments for initialization of a AddMultiplyProcess.
                Has this form: {'multiplier': multiplier}
        """
        # Extract addition multiplier from file.
        file_path = folder_path.joinpath("multi.txt")
        with open(file_path, "r") as file:
            line = file.readline()
            multiplier = int(line)
        return {"multiplier": multiplier}

    def preprocess(self) -> Tuple[int, int]:
        """
        Parse the needed arguments from a request.

        Returns
        -------
        flask.reqparse.Namespace
            Namespace containing the parsed arguments.
        """
        parser = flask_restful.reqparse.RequestParser()
        parser.add_argument("num_1", type=int)
        parser.add_argument("num_2", type=int)
        args = parser.parse_args()
        return args.num_1, args.num_2

    # Ignore pylint error to more closely describe the transfer type between _process
    # and preprocess.
    def _process(
        self, args_list: List[Tuple[int, int]]  # pylint: disable=arguments-differ
    ) -> List[int]:
        """
        Calculate result for a list of input arguments.

        Parameters
        ----------
        args_list
            List of parsed input arguments.

        Returns
        -------
        List
            list of calculation results.
        """
        result_list = [(num_1 + num_2) * self.multiplier for num_1, num_2 in args_list]
        return result_list

    def _postprocess(self, result: int) -> dict:  # pylint: disable=arguments-differ
        """
        Arrange a list of results into a list of dicts that are json serializable.

        Parameters
        ----------
        results
            List of claculation results.

        Returns
        -------
        dict
            Serializable dict has the form: {'result': result}
        """
        return {"result": result}
